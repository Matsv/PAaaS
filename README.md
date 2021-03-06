PAaaS
===================
Minecraft Protocol Analysis as a Service

The idea of PAaaS is to provide a service which can on the fly compare the differences in protocols between 2 versions. This includes regular updating to ensure it has the newest data.

**Hosted version**: https://matsv.nl/PAaaS/

----------
![Preview of the interface](http://myles.us/source/fIVO.png)

Running
-------------
**Requirements:** Java

Use `git clone` and then `mvn install` to make a runnable jar. Otherwise download a copy from our build server.

Build Server: https://ci.viaversion.com/view/All/job/PAaaS/

After executing you will be able to access the tool at http://localhost:8080.

The tool initially make take some time to boot, it runs various git commands to get the required tools to run.

Tools we use in our PAaaS
-------
Burger - https://github.com/mcdevs/Burger/

Jawa - https://github.com/TkTech/Jawa/

mdep - https://github.com/yawkat/mdep/



License
-------
This project uses the MIT license, see [LICENSE.md](https://github.com/Matsv/PAaaS/blob/master/LICENSE.md) for more information.