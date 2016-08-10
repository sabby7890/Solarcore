package org.solarcore.app;

/*
 This file is part of the Solarcore project (https://github.com/sabby7890/Solarcore).

 Solarcore is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Solarcore is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Solarcore.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

class SolarcoreRRD {
    enum DataType {
        LONG,
        INTEGER,
        SHORT,
        BYTE
    }

    enum Period {
        ONE_HOUR,
        FOUR_HOURS,
        DAY,
        WEEK,
        MONTH,
        YEAR,
        TWO_YEARS,
        THREE_YEARS,
        FIVE_YEARS,
        TEN_YEARS
    }

    enum ConsolidationFunction {
        MIN,
        MAX,
        AVG,
        COUNT,
        LAST
    }

    private DataType datatype;
    private Period period;
    private ConsolidationFunction cfunc;
    private int step;
    private long startTime;
    private String id;
    private String targetID;
    private final String fileName;
    private long totalSeconds;

    private RandomAccessFile rFile;

    private SolarcoreLog log;
    SolarcoreRRD(String pId, SolarcoreLog pLog, String pTargetID, DataType pDataType, Period pPeriod, ConsolidationFunction pCFunc, int pStep) {
        targetID = pTargetID;
        id = pId;
        log = pLog;
        datatype = pDataType;
        period = pPeriod;
        cfunc = pCFunc;
        step = pStep;
        startTime = -1;

        fileName = "rrd" + File.separator + targetID + "_" + id + ".rrdata";

        switch (period) {
            case ONE_HOUR:
                totalSeconds = 3600;
                break;
            case FOUR_HOURS:
                totalSeconds = 14400;
                break;
            case DAY:
                totalSeconds = 86400;
                break;
            case WEEK:
                totalSeconds = 604800;
                break;
            case MONTH:
                totalSeconds = 14144000;
                break;
            case YEAR:
                totalSeconds = 31536000;
                break;
            case TWO_YEARS:
                totalSeconds = 63072000;
                break;
            case THREE_YEARS:
                totalSeconds = 94608000;
                break;
            case FIVE_YEARS:
                totalSeconds = 157680000;
                break;
            case TEN_YEARS:
                totalSeconds = 315360000;
                break;
        }

        if (Files.notExists(Paths.get(fileName)))
            createRRData();

        try {
            rFile = new RandomAccessFile(fileName, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        startTime = detectStartTime();

    }

    private void createRRData() {
        startTime = System.currentTimeMillis() / 1000L;
        FileOutputStream outputStream;

        try {
            outputStream = new FileOutputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        DataOutputStream dataStream = new DataOutputStream(outputStream);

        try {
            dataStream.write("RRHEADER".getBytes());
            switch (datatype) {
                case LONG:
                    dataStream.write((byte)0);
                    break;
                case INTEGER:
                    dataStream.write((byte)1);
                    break;
                case SHORT:
                    dataStream.write((byte)2);
                    break;
                case BYTE:
                    dataStream.write((byte)3);
                    break;
            }

            switch (cfunc) {
                case MIN:
                    dataStream.write((byte)0);
                    break;
                case MAX:
                    dataStream.write((byte)1);
                    break;
                case AVG:
                    dataStream.write((byte)2);
                    break;
                case LAST:
                    dataStream.write((byte)3);
                    break;
            }

            dataStream.write(step);


            log.info("Pos: " + outputStream.getChannel().position());
            dataStream.write(SolarcoreSystem.primitiveToBytes(startTime));

            dataStream.write("RRDATA".getBytes());

            for (int i = 0; i <= (totalSeconds / step)+1; i++) {
                switch (datatype) {
                    case LONG:
                        dataStream.writeLong(0);
                        break;
                    case INTEGER:
                        dataStream.writeInt(0);
                        break;
                    case SHORT:
                        dataStream.writeShort(0);
                        break;
                    case BYTE:
                        dataStream.writeByte(0);
                        break;
                }
            }
            dataStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    long consolidate(long prev, long next) {
        switch (cfunc) {
            case MIN:
                return SolarcoreSystem.min(prev, next);
            case MAX:
                return SolarcoreSystem.max(prev, next);
            case COUNT:
                return SolarcoreSystem.count(prev, next);
            case AVG:
                return SolarcoreSystem.avg(prev, next);
            case LAST:
                return SolarcoreSystem.last(prev, next);
            default:
                return 0;
        }
    }

    private int consolidate(int prev, int next) {
        switch (cfunc) {
            case MIN:
                return SolarcoreSystem.min(prev, next);
            case MAX:
                return SolarcoreSystem.max(prev, next);
            case COUNT:
                return SolarcoreSystem.count(prev, next);
            case AVG:
                return SolarcoreSystem.avg(prev, next);
            case LAST:
                return SolarcoreSystem.last(prev, next);
            default:
                return 0;
        }
    }

    private short consolidate(short prev, short next) {
        switch (cfunc) {
            case MIN:
                return SolarcoreSystem.min(prev, next);
            case MAX:
                return SolarcoreSystem.max(prev, next);
            case COUNT:
                return SolarcoreSystem.count(prev, next);
            case AVG:
                return SolarcoreSystem.avg(prev, next);
            case LAST:
                return SolarcoreSystem.last(prev, next);
            default:
                return 0;
        }
    }

    private byte consolidate(byte prev, byte next) {
        switch (cfunc) {
            case MIN:
                return SolarcoreSystem.min(prev, next);
            case MAX:
                return SolarcoreSystem.max(prev, next);
            case COUNT:
                return SolarcoreSystem.count(prev, next);
            case AVG:
                return SolarcoreSystem.avg(prev, next);
            case LAST:
                return SolarcoreSystem.last(prev, next);
            default:
                return 0;
        }
    }

    private long detectStartTime() {
        long starttime = read(11);
        return starttime;
    }

    private long getCurrentFilePosition() {
        long currentTime = System.currentTimeMillis() / 1000L;
        long difference = currentTime - startTime;
        long blockSize;

        switch (datatype) {
            case LONG:
                blockSize = Long.SIZE / 8;
                break;
            case INTEGER:
                blockSize = Integer.SIZE / 8;
                break;
            case SHORT:
                blockSize = Short.SIZE / 8;
                break;
            case BYTE:
                blockSize = Byte.SIZE / 8;
                break;
            default:
                blockSize = 0;
                break;
        }

        if (difference > totalSeconds)
            difference -= difference / totalSeconds*totalSeconds;

        return 25 + (difference / step) * blockSize;
    }

    private long read(long offset) {
        long data = -1;
        try {
            rFile.seek(offset);
        } catch (IOException e) {
            return data;
        }

        try {
            switch (datatype) {
                case LONG:
                    try {
                        data = rFile.readLong();
                    } catch (EOFException e) {
                        log.error("Unable to read offset: " + offset);
                    }

                    break;
                case INTEGER:
                    data = rFile.readInt();
                    break;
                case SHORT:
                    data = rFile.readShort();
                    break;
                case BYTE:
                    data = rFile.readByte();
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    private void write(long offset, long value) {
        try {
            rFile.seek(offset);
        } catch (IOException e) {
            return;
        }

        try {
            rFile.writeLong(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(long offset, int value) {
        try {
            rFile.seek(offset);
        } catch (IOException e) {
            return;
        }

        try {
            rFile.writeInt(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(long offset, short value) {
        try {
            rFile.seek(offset);
        } catch (IOException e) {
            return;
        }

        try {
            rFile.writeShort(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(long offset, byte value) {
        try {
            rFile.seek(offset);
        } catch (IOException e) {
            return;
        }

        try {
            rFile.writeByte(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void put(long i) {
        long pos = getCurrentFilePosition();
        long val = read(pos);
        write(pos, consolidate(val, i));
    }

    void put(int i) {
        long pos = getCurrentFilePosition();
        long val = read(pos);
        write(pos, consolidate((int)val, i));
    }

    void put(short i) {
        long pos = getCurrentFilePosition();
        long val = read(pos);
        write(pos, consolidate((short)val, i));
    }

    void put(byte i) {
        long pos = getCurrentFilePosition();
        long val = read(pos);
        write(pos, consolidate((byte)val, i));
    }

    public String getData(String from, String to, int step) {
        return "RRD DATA";
    }
}