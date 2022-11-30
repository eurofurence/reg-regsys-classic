
default: build

perm:
	chmod 644 src/jsp/*.jsp src/jsp/*.inc
	chmod -R u=rwX,go=rX src/tpl

build: perm 
	./gradlew --no-daemon --console=plain clean war

deploy:
	cp build/libs/regsys-*.war $(INSTBASE)/$(WARNAME)

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
