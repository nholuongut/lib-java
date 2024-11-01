# Java Library

![](https://i.imgur.com/waxVImv.png)
### [View all Roadmaps](https://github.com/nholuongut/all-roadmaps) &nbsp;&middot;&nbsp; [Best Practices](https://github.com/nholuongut/all-roadmaps/blob/main/public/best-practices/) &nbsp;&middot;&nbsp; [Questions](https://www.linkedin.com/in/nholuong/)
<br/>

Java library, full of lots of validation code and utility functions.

## Build

Builds with any one of Maven, Gradle or SBT. The Maven and Gradle builds are best as they will auto-download their own build systems of the correct compatible version for you without you having to pre-install them.

The default build will trigger a Gradle build which requires no pre-installed dependencies other than Java. This is preferred because of the self-bootstrap but the Gradle mechanism additionally has an embedded checksum for security:

```shell
make
```

You can call any one of the 3 major build systems explicitly instead:

Maven:

```shell
make mvn
```

Gradle:

```shell
make gradle
```

SBT:

```shell
make sbt
```

### Testing

Continuous Integration is run on this repo to build and unit test it (around 400 JUnit tests).

You can launch tests manually by running this command at the top level of the repo:

```shell
make test
```

### Configuration

Strict validations include host/domain/FQDNs using TLDs which are populated from the official IANA list, a snapshot of which is shipped as part of this project.

To update the bundled official IANA TLD list with the latest valid TLDs do

```shell
make tld
```

### Custom TLDs

If using bespoke internal domains such as `.local`, `.intranet`, `.vm`, `.cloud` etc. that aren't part of the official IANA TLD list then this is additionally supported via a custom configuration file in [src/main/resources](https://github.com/nholuongut/lib-java/tree/master/src/main/resources) called [custom_tlds.txt](https://github.com/nholuongut/lib-java/blob/master/src/main/resources/custom_tlds.txt) containing one TLD per line, with support for # comment prefixes. Just add your bespoke internal TLD to the file and it will then pass the host/domain/fqdn validations.

# 🚀 I'm are always open to your feedback.  Please contact as bellow information:
### [Contact ]
* [Name: nho Luong]
* [Skype](luongutnho_skype)
* [Github](https://github.com/nholuongut/)
* [Linkedin](https://www.linkedin.com/in/nholuong/)
* [Email Address](luongutnho@hotmail.com)
* [PayPal.me](https://www.paypal.com/paypalme/nholuongut)

![](https://i.imgur.com/waxVImv.png)
![](Donate.png)
[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/nholuong)

# License
* Nho Luong (c). All Rights Reserved.🌟

