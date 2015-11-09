all:
	mkdir -p bin
	javac -d bin -cp src src/*/*/*.java
	javac -d bin -cp src src/*/*/*/*.java
