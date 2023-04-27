
# *****************************************************
# Variables to control Makefile operation.

JCOMPILER = javac
JFLAGS = 

JRUNTIME = java

# ****************************************************
# Targets needed to build the executable from the source folder.

swingClock: swingClock.java Global.java AlarmButton.java
	@if [ ! -d "/snap/openjfx/current" ]; then \
		echo "Error! The openjfx package is not installed, but is required."; \
		echo "   try 'sudo snap install openjfx', then re-run this make."; \
		echo ""; \
		exit 1; \
	fi

	$(JCOMPILER) $(JFLAGS) swingClock.java

	@echo "Build Done !"
	@echo

# ****************************************************
# Target needed to run the executable from the source folder.

run: swingClock
	@if [ ! -d "/snap/openjfx/current" ]; then \
		echo "Error! The openjfx package is not installed, but is required."; \
		echo "   try 'sudo snap install openjfx', then re-run this make."; \
		echo ""; \
		exit 1; \
	fi

	$(JRUNTIME) $(JFLAGS) swingClock

	@echo "Run Done !"
	@echo

# ****************************************************
# Target needed to install the executable.

install: swingClock
ifneq ($(shell id -u), 0)
	@echo "You must be root to perform this action. Please re-run with:"
	@echo "   sudo make install"
	@echo
	@exit 1;
endif

	@echo
	@echo "sudo make install: starts ..."

	# # Kill any active instances. Installed and run-from-dev folder conflict
	# # over shared ~/.java.userPrefs.
	# for FILE in $$(pgrep java) ; do \
	# 	ps -p $$FILE -o args --no-headers | egrep swingClock && kill $$FILE; \
	# done

	mkdir -p /usr/local/swingClock

	cp *.class /usr/local/swingClock

	cp 'okButton.png' /usr/local/swingClock
	cp 'cancelButton.png' /usr/local/swingClock
	cp 'alarmBeep.wav' /usr/local/swingClock

	cp 'swingclock.desktop' /usr/share/applications/
	cp 'swingclock.png' /usr/share/icons/hicolor/48x48/apps/

	sudo -u ${SUDO_USER} \
		rm -rf /home/${SUDO_USER}/.java/.userPrefs/swingClock

	@echo "Install Done !"
	@echo

# ****************************************************
# Target needed to uninstall the executable.

uninstall:
ifneq ($(shell id -u), 0)
	@echo "You must be root to perform this action. Please re-run with:"
	@echo "   sudo make uninstall"
	@echo
	@exit 1;
endif

	# # Kill any active instances.
	# for FILE in $$(pgrep java) ; do \
	# 	ps -p $$FILE -o args --no-headers | egrep swingClock && kill $$FILE; \
	# done

	@echo ""
	@echo "sudo make uninstall: starts ..."

	rm -rf /usr/local/swingClock

	rm -f /usr/share/applications/swingclock.desktop
	rm -f /usr/share/icons/hicolor/48x48/apps/swingclock.png

	sudo -u ${SUDO_USER} \
		rm -rf /home/${SUDO_USER}/.java/.userPrefs/swingClock

	@echo "Uninstall Done !"
	@echo

# ****************************************************
# Target needed to clean the source folder for a fresh make.

clean:
	rm -f *.class

	rm -rf ~/.java/.userPrefs/swingClock

	@echo "Clean Done !"
	@echo
