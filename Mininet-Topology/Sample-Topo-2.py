#!/usr/bin/python

from mininet.net import Mininet
from mininet.node import Controller, RemoteController
from mininet.cli import CLI
from mininet.log import setLogLevel, info
from mininet.node import OVSSwitch
from mininet.link import TCLink
from mininet.link import Intf

def emptyNet():

    NODE2_IP='10.0.0.40' # Remote computer IP

    net = Mininet( topo=None,
                   build=False, switch=OVSSwitch, link=TCLink)

    net.addController( 'c0',
                      controller=RemoteController)

    h3 = net.addHost( 'h3' )
    
    h4 = net.addHost( 'h4' )
    h5 = net.addHost( 'h5' )

    s2 = net.addSwitch( 's2' )
  
    net.addLink( h4, s2 , bw = 10)
    net.addLink( h3, s2 , bw = 10)
    

    net.addNAT().configDefault()
    net.start()

    #GRE tunnel
    s2.cmd('ovs-vsctl add-port s2 s2-gre1 -- set interface s2-gre1 type=gre options:remote_ip='+NODE2_IP)

    CLI( net )
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )
    emptyNet()
