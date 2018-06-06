#!/usr/bin/python

from mininet.net import Mininet
from mininet.node import Controller, RemoteController
from mininet.cli import CLI
from mininet.log import setLogLevel, info
from mininet.node import OVSSwitch
from mininet.link import Intf

def emptyNet():

    NODE2_IP='10.0.0.50' #Remote host IP

    net = Mininet( topo=None,
                   build=False)

    net.addController( 'c0',
                      controller=RemoteController)

    h1 = net.addHost( 'h1' )
    
    h2 = net.addHost( 'h2' )

    s1 = net.addSwitch( 's1' )
  
    net.addLink( h1, s1 )
    net.addLink( h2, s1 )


    net.addNAT().configDefault()

    net.start()

    #GRE tunnel
    s1.cmd('ovs-vsctl add-port s1 s1-gre1 -- set interface s1-gre1 type=gre options:remote_ip='+NODE2_IP)

    CLI( net )
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )
    emptyNet()
