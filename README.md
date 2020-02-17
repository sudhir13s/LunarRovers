# Lunar Rover - Final project

README.md
==========================

### Introduction: 
The LunarRover project
Part 1: Implemented RIPv2 protocol to enable reover to send each other routing table.
Part 2: Implemented a protocol (Lunar Rover Protocol) to send data.
Part 3: Combined the project part 1 and 2 to complete the project and send the data through one Rover to another rover.

### Requirements: 
Please follow the sample docker files (README-1.md) provided as part of this project. Also, please refer to the Lunar Rover Protocol for protocol design.
Also, as part of this project, below cases has been assumed.

    Multicast address: 224.0.0.9 
    (This is the address on which all Rover will join to send/receive multicast messages.)

    Multicast Port: 5520 
    (This is the port of Multicast messages.)

    RIP Port: 5521 
    (This is the port in which regurals updates/routing tables are sent.)

    Internal subnet: 10.0.subnet.0/24
    The subnet id is provided as a command line argument. The subnet is suumed to be 255.255.255.0 for part 1 of the project.

    RIP command = 2
    As part of the project, only command = 2 (response) has been implemented as it was only required (command=1 request not needed).

    LRP Port: 45654
    (This is the port used for sending data between Rovers over LRP protocol.)

    Sender SEQ start: 100
    Receiver SEQ start: 1000

    The main program waits for 30 seconds to let all routes settle down before sending the data to destination.
    The Sender has socket timeout of 500ms due to testing with different network and updation of routes in fly.
    The transfer log of sending and receiving files are logged on console for better view.
    The file received at the destination has predefined file name as "LunarRoverProtocolFile".



### Description:
The project is architect to devide the task between threads. Each thread execute a task. Below are the details-

    a) LunarRover : This is the main program of a Rover which need to be executed. Other sub-programs/file/threads will be executed with this program.
    b) HostUpdate : The program to create and update subnet in a Rover.
    c) RIP Trigger Update: The program/thread to send trigger updates to multicast address.
    d) RIP Multicast Receiver: The program/thread to receive all multicast messages on a specific Rover.
    e) RIP Receiver: The program/thread to receive regular routing table updates.
    f) RIP Sender: The program/thread to send regular routing table updates on every 5 seconds. 
    g) Garbage Collector: The program/thread to clean/update the routing table of a Rover.
    h) LRP: The LRP protocol designed as part of the project.
    i) LRP Sender: The program/thread to send data to another Rover
    j) LRP Receiver: The program/thread to route or receive the data from another Rover.


### Execution of the program.<br>
    
    1) Please follow the docker files provided (README-1.md file for more details) as part of the project to test different scenarios.
    2) Please follow the project files (provided as part of assignment) for further details on execution of the final program.


### Sample Output of a Rover:
A large file sent from a Rover will be received at destinated Rover with the file name "LunarRoverProtocolFile".
