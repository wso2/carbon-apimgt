###########################################  READ ME ############################################
                                                                                                #
Change 1                                                                                        #
--------------------                                                                            #
The default value of DCID column in IDN_OAUTH2_ACCESS_TOKEN table should be changed based on    #
the DC. The script for the above table can be found in apimgt/tables.sql. An Example is         #
given below for a deployment with 2 DC's.                                                       #
                                                                                                #
DC1                                                                                             #
DCID VARCHAR2 (50) DEFAULT 'DC1',                                                               #
                                                                                                #
DC2                                                                                             #
DCID VARCHAR2 (50) DEFAULT 'DC2',                                                               #
                                                                                                #
Change 2                                                                                        #
--------------------                                                                            #
Start value and increment for IDENTITY columns in tables should be altered to create a unique   #
number for each DC based on the number of DB instances in your environment. The IDENTITY        #
keyword and it's bindings are responsible for creating auto increment integers which act as the #
primary keys for relevant tables. Unless they are altered to create a unique numbers based on   #
the DC, it can create a unqiue constraint violation when Replicating DBs.                       #
                                                                                                #
After the keyword "IDENTITY", in the brackets there are two values.                             #
(StartingValue, NumberOfNodes)                                                                  #
                                                                                                #
The  start value should be set as the instance id of the particular DC and increment value      #
should be set based on the number of DC in your deployment. The below example shows how to      #
alter the queries on a sample scenario where you have 3 master DB instances in 3 DC's           #
                                                                                                #
DC1 TABLES                                                                                      #
ID INTEGER IDENTITY(1,3)                                                                        #
                                                                                                #
DC2 TABLES                                                                                      #
ID INTEGER IDENTITY(2,3)                                                                        #
                                                                                                #
DC3 TABLES                                                                                      #
ID INTEGER IDENTITY(3,3)                                                                        #
                                                                                                #
For more details refer                                                                          #
(https://wso2.com/library/article/deploying-wso2-api-manager-in-a-multi-dc-environment/)        #
#################################################################################################