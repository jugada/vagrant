vagrant
=======
Use this repo to setup your development machine, it includes the cyodm play application (please keep using this app for any development)

It will setup a virtual box with: 

- CentOS 5.7 x86_64
- Latest Java
- Play Framework 2.1.1


Yo will find the application files in your host (local) machine under `/play/cyodm` and the same files in your virtual machine under `/vagrant/play/cyodm`

Installation instructions:
=======

- Clone this repo to your local machine
- Download latest version of Vagrant
- Download latest version of VirtualBox
- Type `vagrant up`
- It will download the box and make the proper installations, this may take up to 30 mins depending on your internet connection
- Connect to your new virtual box using `vagrant ssh`
- Add play to the local path using 
  `echo 'export PATH=$PATH:/etc/play/play-2.1.1' >> $HOME/.bash_profile;
    . $HOME/.bash_profile`
- Add Neo4J to local path using `echo 'export PATH=$PATH:/neo4j/neo4j-community-1.9/bin' >> $HOME/.bash_profile;
    . $HOME/.bash_profile`

After that navigate to `/vagrant/play/cyodm` and type `sudo play` and the play console will show up. You can start the application by typing `run` in the console. Verify the app is running by visiting `localhost:9090` on your host machine. You should see the default application page. 

To start the neo4J database server type `neo4j start`
