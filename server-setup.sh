# new user

sudo adduser jaacko-torus
sudo usermod -aG sudo jaacko-torus
sudo su - jaacko-torus

sudo ufw allow 80/tcp
sudo ufw allow 8080/tcp
sudo ufw allow 8081/tcp

# asdf

git clone https://github.com/asdf-vm/asdf.git ~/.asdf --branch v0.10.2
echo -e "\n#asdf" >> ~/.bashrc
echo ". $HOME/.asdf/asdf.sh" >> ~/.bashrc
echo ". $HOME/.asdf/completions/asdf.bash" >> ~/.bashrc
bash

# initial software

# java
asdf plugin add java
asdf install java temurin-11.0.15+10
asdf global java temurin-11.0.15+10
# sbt
asdf plugin add sbt
asdf install sbt 1.6.2
asdf global sbt 1.6.2
# scala
asdf plugin add scala
asdf install scala 2.13.8
asdf global scala 2.13.8

# seiner

git clone https://github.com/jaacko-torus/seiner.git
cd seiner
sbt run
