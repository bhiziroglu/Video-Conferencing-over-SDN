#!/usr/bin/python

from mininet.net import Mininet
from mininet.node import Controller, RemoteController
from mininet.cli import CLI
from mininet.log import setLogLevel, info
from mininet.node import OVSSwitch
from mininet.link import TCLink

def emptyNet():

    NODE2_IP='10.0.0.50'

    net = Mininet( topo=None,
                   build=False, switch=OVSSwitch, link=TCLink)

    net.addController( 'c0',
                      controller=RemoteController)


    s11 = net.addSwitch( 's11' )
    s12 = net.addSwitch( 's12' )
    s13 = net.addSwitch( 's13' )
    s14 = net.addSwitch( 's14' )
    s15 = net.addSwitch( 's15' )
    s16 = net.addSwitch( 's16' )
    s17 = net.addSwitch( 's17' )
    s18 = net.addSwitch( 's18' )
    s19 = net.addSwitch( 's19' )
    s20 = net.addSwitch( 's20' )


    
    h11 = net.addHost( 'h11' ,ip='10.0.0.20')
    h12 = net.addHost( 'h12' ,ip='10.0.0.21')
    h13 = net.addHost( 'h13' ,ip='10.0.0.22')
    h14 = net.addHost( 'h14' ,ip='10.0.0.23')
    h15 = net.addHost( 'h15' ,ip='10.0.0.24')
    h16 = net.addHost( 'h16' ,ip='10.0.0.25')
    h17 = net.addHost( 'h17' ,ip='10.0.0.26')
    h18 = net.addHost( 'h18' ,ip='10.0.0.27')
    h19 = net.addHost( 'h19' ,ip='10.0.0.28')

    '''
    EDGES (from-node to-node length a b):
    0 8 4 0 
    0 7 6 0 
    0 6 4 0 
    0 3 4 0 
    0 1 9 0 
    1 6 6 0 
    1 2 10 0 
    2 7 2 0 
    4 9 6 0 
    4 8 5 0 
    4 6 6 0 
    5 9 6 0 
    6 9 1 0 
    6 8 1 0 
    6 7 7 0 
    '''
    ## ADD EDGES
    net.addLink( s11, s19 )
    net.addLink( s11, s18 )
    net.addLink( s11, s17 )
    net.addLink( s11, s14 )
    net.addLink( s11, s12 )
    net.addLink( s12, s17 )
    net.addLink( s12, s13 )
    net.addLink( s13, s18 )
    net.addLink( s15, s20 )
    net.addLink( s15, s19 )
    net.addLink( s15, s17 )
    net.addLink( s16, s20 )
    net.addLink( s16, s19 )
    net.addLink( s16, s18 )
    

    net.addLink( s11, h11)
    net.addLink( s12, h12)
    net.addLink( s13, h13)
    net.addLink( s14, h14)
    net.addLink( s15, h15)
    net.addLink( s16, h16)
    net.addLink( s17, h17)
    net.addLink( s18, h18)
    net.addLink( s19, h19)
    
	#Uncomment the following line to allow NAT - Can be used to connect hosts to SDN Controller
    #net.addNAT().configDefault()
    net.start()


    #Uncomment the following line to configure the GRE tunnel
    #s14.cmd('ovs-vsctl add-port s14 s14-gre1 -- set interface s14-gre1 type=gre options:remote_ip='+NODE2_IP)

    CLI( net )
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )
    emptyNet()
