JFLAGS = -g
JAVAC  = javac
RM     = rm

TARGET  = BasicGA.class
SOURCES = BasicGA.java

all: $(TARGET)
run : $(TARGET)
	java $(SOURCES)

.SUFFIXES: .java .class
.java.class:
	$(JAVAC) $(JFLAGS) $<

clean:
	-$(RM) $(SOURCES:.java=.class)
	-$(RM) *.out