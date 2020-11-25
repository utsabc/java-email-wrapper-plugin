# java-email-wrapper-plugin
A simple email wrapper plugin for reading mails in simple POJO models

### Add dependecy to the pom.xml

```
<dependency>
  <groupId>com.email.wrapper.api</groupId>
  <artifactId>email-wrapper-api</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

### Enable Access from github packages 

To enable maven to read the artifact from github please set up/ modify your settings.xml in .m2/settings.xml.
This specific configuration ensure access from both maven central repository also from github packages.
```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <activeProfiles>
    <activeProfile>github</activeProfile>
  </activeProfiles>

  <profiles>
    <profile>
      <id>github</id>
      <repositories>
        <repository>
          <id>central</id>
          <url>https://repo1.maven.org/maven2</url>
          <releases><enabled>true</enabled></releases>
          <snapshots><enabled>true</enabled></snapshots>
        </repository>
        <repository>
          <id>github</id>
          <name>GitHub Apache Maven Packages</name>
          <url>https://maven.pkg.github.com/utsabc/java-email-wrapper-plugin</url>
        </repository>
      </repositories>
    </profile>
  </profiles>

  <servers>
    <server>
      <id>github</id>
      <username>utsabc</username>
      <password>${{ secrets.SECRET_NAME }}</password>
    </server>
  </servers>
</settings>
```
## API Usage

```
EmailFactory factory = new EmailFactory(new EmailConfiguration.ConfigBuilder()
					.setUser("your.email@email.com")
					.setPassword("YourPassword")
					.setConnection("IMAP")  //Can be set to IMAP/POP3
					.setHost("imap.gmail.com")  //Specific host based on imap/pop3 and provider
					.setPort("993")  //specific to IMAP/POP3 (110,993,995)
					.build(), null);  //(NULL by default will set it to READ MODE) see javax.mail.event.ConnectionEvent
			  factory.setStartIndex(0); //Sets the starting index
			  factory.setEndIndex(100); //Sets the ending index
		Collection<EmailPOJO> emails =  factory.read("Inbox");
```
