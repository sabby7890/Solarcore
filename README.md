Solarcore
=========

Solarcore is a monitoring system provided to be a replacement for Nagios/Icinga on smaller systems. As a Java application, Solarcore will run on any system supporting Java. 

Solarcore is designed to be easy to use. This means, no config file editing is necessary - user can click and add a target to monitor, then add probes. Probes supported in version 1.0 will include:

- PING probe (ICMP based probe, uses PING command on supported systems)
- TCP probe - connects to TCP port and checks for answer
- Solarcore probe - connect to Python-based daemon running on target system to monitor health (processes, CPU, RAM, disk)
- SNMP probe - performs SNMP query on target
- MySQL probe - performs MySQL query on target
- HTTP Probe - measure HTTP response time
- DNS probe - monitors DNS response time
- Custom probe - connects to specified port on the host and waits for reply

Solarcore includes a fancy web user interface, designed to fit on big screens in IT offices. It also includes an installer, which will allow easy setup.

In the future releases, Solarcore will include a web api and Android application to receive alerts. Currently supported notification systems are email and custom command.
