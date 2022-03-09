# Introduction

In a nutshell, Vamos is a system for creating visual overlays for 3rd party computer software, and for monetizing those overlays. For example, it can be used to add a crosshair or other information to a video game. Additionally, with the proper hardware, the overlay can be shown without installing any software whatsoever on the target machine.

<span style="color:green;font-weight:700">You can make money writing overlays for Vamos!</span> You charge as much or as little as you like, and you receive 90% of the chosen price (I get the other 10% for developing Vamos).

![](wiki-support/ac.png)

# Pricing

Vamos itself is completely free. Overlay authors, however, can charge for their overlay,
and payment is made using the Nano (XNO) cryptocurrency. These payments encourage overlay
authors to produce quality overlays, and to keep their overlays up to date. The reason I
chose Nano is that it is fast, and charges zero transaction fees.

You can get some free Nano for running the sample overlays by using a [Nano faucet](https://hub.nano.org/i/faucets/215).

# Two-Machine Installation

While it is entirely possible to install everything on a single machine, it is not recommended installing
Vamos! on any machine where you play online video games. While the overlay will function, the game itself
may not. The anti-cheat component of modern video games may detect Vamos!,
and disallow play with Vamos! running. If you wish to run an overlay on a gaming machine, it is instead
recommended investing in a PCILeech-compatible memory acquisition device. A list of supported acquisition
devices can be found on the <a href="https://github.com/ufrisk/pcileech#readme">PCILeech website</a>. In this
way, a Vamos! overlay can be used without ever installing any software on the target machine.

Therefore, in this guide, I will describe the procedure to set up Vamos! with a hardware memory acquisition
device. I will use the following terminology:

<ul class="bodyText">
    <li>The <b>source machine</b> is the machine running Vamos!, Tor Browser, MemProcFS, and ceserver-pcileech</li>
    <li>The <b>target machine</b> is the machine with the hardware memory acquisition device; this is
        the machine that is running your video game or other software that you wish to overlay upon. 
        <b><i>No software should ever need to be installed on this machine.</i></b>
    </li>
</ul>

A total of 4 components will need to be installed on the <b>source</b> machine:

<ul class="bodyText">
    <li><b>Vamos!</b> - The main program you will use to download and launch overlays, and to interact with the
        Vamos! ecosystem.
    </li>
    <li><b>Tor Browser</b> - Used to protect privacy when downloading overlays, and when updating Vamos! itself.</li>
    <li><b>MemProcFS</b> - The PCILeech software that runs on the source machine, that talks to the
        hardware memory acquisition device installed in the target machine, typically over a USB-C cable. Please
        ensure that MemProcFS is configured properly, and works in a standalone fashion,
        before trying to use it with Vamos!.
    </li>
    <li><b>ceserver-pcileech</b> - Provides memory acquisition services between the source and target machines.</li>
</ul>

At this point, you will have two machines linked with a USB-C cable via the hardware memory acquisition device.
Launching an overlay will display that overlay on the source machine's screen, but you really want the overlay
to display on the target machine's screen, overlaying the content on that screen. There are a few ways
to do this:

<ul class="bodyText">
    <li>Steam Remote Play, Remote Desktop, or any other program that lets you cast the screen of the target machine
        onto the source machine.
    </li>
    <li>Use a video capture card to cast the target machine's display onto the source machine so the overlay can be drawn on it.</li>
    <li>Use a video mixer to mix the source and target video signals onto a single monitor. The mixer should support chroma-key,
        and the transparency chroma color should be set to black. The target machine's display will show through any
        black pixels of the overlay running on the source machine.</li>
</ul>

![](wiki-support/mixer.png)

Keep in mind that any screen casting solution will introduce some latency, so you will want to do your homework
to find the solutions with the least latency. With too much latency, certain games such as First Person
Shooters may become difficult to play, while other types of games are less affected by latency issues.

# Developing an Overlay

For a developer, Vamos provides:

<ul>
    <li>integrated cryptocurrency subsystem, so you get paid</li>
    <li>TOR support to protect your anonymity</li>
    <li>a trust model, including sandboxed code and digital signatures</li>
    <li>FPGA/DMA support for customers running a dual-box setup</li>
    <li>a consistent API for accessing process memory</li>
    <li>easy distribution and updates via VAMOS urls</li>
</ul>

You can use my [sample overlay](https://github.com/isabellaflores/sample-overlay) as a starting point for your own overlay.

Overlays are written in Java. They are run within a restrictive Java sandbox on the user's machine, so you will not be able to write files or make network connections in your overlay code. An API is available to your overlay to perform the following functions:

<ul>
    <li>Fetch the list of running processes on the target machine</li>
    <li>Read the memory of any process on the target machine</li>
    <li>Store arbitrary data in a special cache that persists between launches</li>
</ul>

Based on this information, you can use Java's Swing UI toolkit to draw your overlay on the source machine's screen. Included in the Vamos SDK is a special <b>iflores.vamos.OverlayFrame</b> class which produces a transparent window in which you can draw your overlay for the user.

Follow these steps to create your own custom overlay:

<ol>
    <li>Check out my [sample overlay](https://github.com/isabellaflores/sample-overlay) as a starting point.</li>
    <li>Create your custom overlay by modifying the overlay code.</li>
    <li>Make sure the 'dev server' is running first by clicking the appropriate button in the <b>Developer Tools</b> window within Vamos.</li>
    <li>Test your overlay by running the <b>iflores.vamos.VamosMain</b> main class within your IDE, passing your overlay subclass name as args[0].</li>
    <li>Edit the TXT file in the <b>pkg</b> subdirectory, providing a description of your overlay.</li>
    <li>Edit the JSON file in the <b>pkg</b> subdirectory, providing per-diem pricing and other info.</li>
    <li>Build your overlay by using the Maven 'package' goal. The resulting JAR file is placed into the <b>pkg</b> subdirectory for you.</li>
</ol>

In order to distribute your overlay, you must perform the following steps:

<ol>
    <li>Open the Developer Tools window within Vamos!</li>
    <li>Generate a new identity if you haven't already done so. <b>Make a copy of this identity using the 'Export Identity' button, and store it somewhere safe.</b> If you lose this identity, you will lose the ability to provide updates to your overlay.</li>
    <li>Cryptographically sign your package using the 'Sign Overlay' button. You will only need to select the <b>.json</b> file in the dialog, but all 3 files will be signed. The signing process will create 3 additional <b>.signature</b> files, for a total of 6 files now in the <b>pkg</b> directory.</li>
    <li>Copy these 6 files to a web server, which <b><i>may</i></b> be behind a TOR hidden service (.onion domain), since the user is guaranteed to have an operational TOR connection.</li>
    <li>Locate the web URL of the <b>.json</b> file</li>
    <li>Remove the <b>.json</b> suffix from the URL - this is your metadata base url</li>
    <li>Click the "Make Vamos URL" button in the Developer Tools window of Vamos, and paste this metadata base url to produce your vamos: url.</li>
    <li>Distribute this vamos: URL to people who wish to download your overlay.</li>
</ol>

The vamos: URL has embedded in it your cryptographic public key. This ensures download integrity, and even if your web server is hacked, it is impossible for the hacker to replace your software with a rogue copy. This URL will remain unchanged across updates to your overlay, and only needs to be generated once. Anyone using the URL will always get the latest deployed version of your overlay.