# tftpClient
An implementation of Trivial File Transfer Protocol (TFTP) client. Refer RFC 1350.

-Compile and run with no command line arguments.

CONNECT - 
The client expects the keyword connect to initiate any transfer. 
Typing connect<space>hostname will connect to host.
Typing connect and hitting return will prompt the user to enter host on next line.
Exceptions will be shown on illegal hostnames, and user will have to connect again in that case.

GET - 
Just typing the keyword get and hitting return will prompt the user to enter filename on next line. 1 filename per line. Unknown file name exception will be thrown if the client does not have the file.
Typing get<space>filename will initiate transfer if host has file, or will throw error otherwise.
Files are saved in the same folder as the .java file.

QUIT -
Typing quit and hitting return will exit the program.

TIMEOUT - 
Timeouts for uninitiated transfer has been set at 5 seconds. 

ERROR HANDLING - 
Errors returned by tftp server are displayed as they should be. User is prompted to enter a command when an error of this kind is shown.
Unknown host exceptions are handled by the client.
