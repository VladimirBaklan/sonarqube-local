FROM sonarqube:8.2-community

COPY plugins/target/plugins*.jar /opt/sonarqube/extensions/plugins
CMD ["./bin/run.sh"]
