
# clamav-client

[![Build Status](https://travis-ci.org/hmrc/clamav-client.svg?branch=master)](https://travis-ci.org/hmrc/clamav-client) [ ![Download](https://api.bintray.com/packages/hmrc/releases/clamav-client/images/download.svg) ](https://bintray.com/hmrc/releases/clamav-client/_latestVersion)

## Local Installation

This requires [ClamAV](http://www.clamav.net/) to be installed the best way to do this is run the [docker-clamav image](https://hub.docker.com/r/mkodockx/docker-clamav). Alternatively use the manual instructions below.

### Configuring hosts file

If you have installed ClamAV manually or are using the Docker image with a native docker installation then add this to your /etc/hosts file.

```127.0.0.1       avscan```

If you are using the docker image and using Docker Machine on Mac then the host IP should be the IP of your Docker Machine IP.

```DOCKER_IP       avscan```

## Configuring your MicroService

### Manual installation on Mac OS with Brew

```brew install clamav```

You can find a slightly longer explaination [here](https://gist.github.com/zhurui1008/4fdc875e557014c3a34e) but make note of the comments as the instructions contain some issues.

Make sure clamd.conf has

```LocalSocket /usr/local/var/run/clamav/clamd.sock```
```TCPSocket 3310```

###### To use clamav-client 
Add the latest released version of the clamav-client to your app dependencies of your micro service build

e.g. ```"uk.gov.hmrc" %% "clamav-client" % "```[```version```](https://bintray.com/hmrc/releases/clamav-client/_latestVersion)```"```

Your _**application.conf**_ should be configured to enable clamav scanning


```JavaScript
clam.antivirus {            
    enabled = true          
    chunkSize = 32768       
    host = avscan           
    port = 3310             
    timeout = 5000          
    threadPoolSize = 20     
    maxLength = 10485760    
}
```

Wire up your microservice to load the ClamAvConfig

```JavaScript
object ClamAvConfiguration extends RunMode {

  lazy val config: Option[Configuration]  = Play.current.configuration.getConfig(s"$env.clam.antivirus")
  lazy val clamAvConfig = LoadClamAvConfig(config)

}
```

Use ClamAntiVirus

```JavaScript
def clamAv: ClamAntiVirus = new ClamAntiVirus()(ClamAvConfiguration.clamAvConfig)
clamAv.send(stream)
clamAv.checkForVirus()
```
