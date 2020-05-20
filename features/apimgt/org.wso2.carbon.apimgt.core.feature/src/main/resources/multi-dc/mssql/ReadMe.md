

##Different aspects of database replication and how to implement those multiple datacenters (DC).

##1. Unique keys for all tables

This is already handled in default multi-dc MSSQL scripts in API Manager. So, you don’t have to introduce any new unique or primary keys to any table in it.

##2. Handle auto-increment columns

In API Manager, this task handled by using Start value variation method.In this approach, each master node is given a different starting value, and each of them increments the auto-increment column by the number of the master nodes in the system. For example, if there are three master nodes in the system, the starting values of those three will be 1, 2, and 3, respectively. All of these will increment their values by 3, which is the number of master nodes in the system. So the three sequences will be like this. 

Node 1 - 1,4,7,10,…

Node 2 - 2,5,8,11,...

Node 3 - 3,6,9,12,...

As you can see, it never gives the same number for any two nodes, and that avoids conflicts. 

``Steps to Produce:
	* At the start of tables.sql files there are list of tables under "Tables need to be edited". At those tables primary key is defined with following Keyword "IDENTITY".
	* After the keyword "IDENTITY", in the brackets there are two values. (StartingValue, NumberOfNodes)
	* Change these values as described in the example above.
	Eg : Assume that your nodes Starting Value is 2 and number of nodes in the eco system is 4,
			ID INTEGER IDENTITY( 2, 4), ``


##3. Remove Cascade Operations

In API Manager this cascading operations are already removed and functionality of cascading operations are implemented using triggers.

##4. Avoid access token conflicts between datacenters

Make sure the cofiguration is done correctly.