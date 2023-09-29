
# [Quark Engine](https://quark.greenscreens.ltd/).

![GitHub release (latest by date)](https://img.shields.io/github/v/release/greenscreens-io/quark-java?style=plastic)
![GitHub](https://img.shields.io/github/license/greenscreens-io/quark-java?style=plastic)

![Compile](https://github.com/greenscreens-io/quark-java/workflows/Compile/badge.svg?branch=master) 
![CodeQL](https://github.com/greenscreens-io/quark-java/workflows/CodeQL/badge.svg)

Visit project web page [here](https://quark.greenscreens.ltd/).

Just as quarks are building blocks which glues subatomic particles into atoms,
Green Screens Quark Engine is a small, lite and fast elementary building block between web and Java server side.

The Quark Engine is a JavaScript and Java web library to enable dynamic remote calls of exposed Java Methods.

Instead of making rest or plain JSON calls, Quark Engine is simplifying this process by allowing
to call Java Server Classes and methods as they as running locally in the browser without
programmers worrying about underlying REST/WebSocket data structures.

Supported channels are WebSocket and HTTP/S operations. Data can be automatically encrypted
by the browser Crypto API protecting JSON data structures even TLS/SSL is not used by using
[CryptoAPI WebAssembly](https://github.com/greenscreens-io/cryptowasm) polyfill.

Base concept is to create Controller classes annotated with Ext* Annotations which will
instruct CDI engine and provided WebSocket or Servlet what to expose.

At front-end part, all what is required is to include small JavaScript lib, part of the Quark Engine.
JavaScript engine is only 7KB in size, and its main purpose is to retrieve signature list of defined exposed
Java Classes/Methods, then to generate and link internal calling mechanism.

To see how to use it, visit project web page [here](https://quark.greenscreens.ltd).

### Important!!!
 - To use "javax" version for older Java EE8 servers, use Quark 5.0.0.
 - To use "jakarta" version for Java Jakarta EE, use Quar 6.0.0. or newer.

### Build

1. Clone repository to local drive
2. Use ```maven build clean install```
3. Optionally, import demo and io.greenscreens.quark projects into Eclipse

&copy; Green Screens Ltd. 2016 - 2023
