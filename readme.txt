1. Test connection -- mongo "mongodb+srv://mflix-43fjf.mongodb.net/test" --username m220student
2. Cluster URL -- mflix-43fjf.mongodb.net
3. Import data using mongorestore
	mongorestore --drop --gzip --uri mongodb+srv://xxxxx:******@mflix-43fjf.mongodb.net data
	Replace ****** with your mongo db user password
4. Setup JWT authentication in Spring boot 
	