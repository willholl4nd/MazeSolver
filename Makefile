JFLAGS = -g 
JC = javac
JVM = java
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	  Main.java \
	  Timer.java

MAIN = Main

default: classes

classes: $(CLASSES:.java=.class)

run: $(MAIN).class
	$(JVM) $(MAIN) 1000.png

test: $(MAIN).class
	$(JVM) $(MAIN) 100.png
	$(JVM) $(MAIN) 500.png
	$(JVM) $(MAIN) 1000.png
	$(JVM) $(MAIN) 1500.png
	$(JVM) $(MAIN) 2000.png
	$(JVM) $(MAIN) 2500.png
	$(JVM) $(MAIN) 3000.png
	$(JVM) $(MAIN) 3500.png
	$(JVM) $(MAIN) 4000.png
	$(JVM) $(MAIN) 4500.png
	$(JVM) $(MAIN) 5000.png
	$(JVM) $(MAIN) 5500.png
	$(JVM) $(MAIN) 6000.png
	$(JVM) $(MAIN) 6500.png
	$(JVM) $(MAIN) 7000.png
	$(JVM) $(MAIN) 7500.png
	$(JVM) $(MAIN) 8000.png
	$(JVM) $(MAIN) 8500.png
	$(JVM) $(MAIN) 9000.png

clean:
	rm *.class
