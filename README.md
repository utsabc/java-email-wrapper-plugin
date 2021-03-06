# java-email-wrapper-plugin
A simple email wrapper plugin for reading mails in simple POJO models

Easily read emails from inboxes without the hastle of setting up internal connections, handling exceptions and complex mime-type conversions for attachments,
returns mails in simple JAVA POJO based models along with attachments.

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

Simple Code that prints all the emails to console. EmailPOJO is made generic to support further usecases.
![API](/Isage.PNG?raw=true "Title")

