ANT=ant

.DEFAULT:
	@#echo "*** '$(ANT) $<'" instead
	$(ANT) $<

all: compile
