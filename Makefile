
default: build

perm:
	chmod 644 src/jsp/*.jsp
	chmod -R u=rwX,go=rX src/tpl
	chmod 755 ./gradlew

build: perm 
	./gradlew --no-daemon --console=plain clean war

deploy:
	cp build/libs/reg-regsys-classic-*.war $(INSTBASE)/$(WARNAME)

undeploy:
	rm -f $(INSTBASE)/$(WARNAME)

install: build deploy

stop:
	sudo /etc/init.d/tomcat8 stop

start:
	sudo /etc/init.d/tomcat8 start

restart:
	sudo /etc/init.d/tomcat8 restart

docker: build
	docker build -t regsys
