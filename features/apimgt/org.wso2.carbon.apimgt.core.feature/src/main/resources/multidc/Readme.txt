###########################################  READ ME ############################################
                                                                                                #
The sequences in sequence.sql file should be altered to create a unique number for each DC      #
based on the number of DB instances in your enviroment. The above sequences are responsible     #
for creating auto increment integers which act as the primary keys for relevant tables. Unless  #
they are altered to create a unique numbers based on the DC, it can  create a unqiue constraint #
violation when Replicating DBs.                                                                 #
                                                                                                #
The sequences start value should be set as the instance id of the particular DC and increment   #
value should be set based on the number of DC in your deployment. The below example shows how   #
to alter the sequences on a sample scenario where you have 3 master DB instances in 3 DC's      #
                                                                                                #
DC1                                                                                             #
CREATE SEQUENCE REG_LOG_SEQUENCE START WITH 1 INCREMENT BY 3 NOCACHE                            #
                                                                                                #
DC2                                                                                             #
CREATE SEQUENCE REG_LOG_SEQUENCE START WITH 2 INCREMENT BY 3 NOCACHE                            #
                                                                                                #
DC3                                                                                             #
CREATE SEQUENCE REG_LOG_SEQUENCE START WITH 3 INCREMENT BY 3 NOCACHE                            #
                                                                                                #
#################################################################################################