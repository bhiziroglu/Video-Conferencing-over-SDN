#!/usr/bin/python

from mininet.net import Mininet
from mininet.node import Controller, RemoteController
from mininet.cli import CLI
from mininet.log import setLogLevel, info
from mininet.node import OVSSwitch
from mininet.link import TCLink

def emptyNet():

    NODE2_IP='10.0.0.200'

    net = Mininet( topo=None,
                   build=False, switch=OVSSwitch, link=TCLink)

    net.addController( 'c0',
                      controller=RemoteController)


    s1 = net.addSwitch( 's1' )
    s2 = net.addSwitch( 's2' )
    s3 = net.addSwitch( 's3' )
    s4 = net.addSwitch( 's4' )
    s5 = net.addSwitch( 's5' )
    s6 = net.addSwitch( 's6' )
    s7 = net.addSwitch( 's7' )
    s8 = net.addSwitch( 's8' )
    s9 = net.addSwitch( 's9' )
    s10 = net.addSwitch( 's10' )

    '''
    EDGES (from-node to-node length a b):
    0 9 4 0 
    0 4 6 0 
    0 2 4 0 
    1 8 7 0 
    2 6 7 0 
    3 9 2 0 
    3 8 7 0 
    3 5 7 0 
    4 7 7 0 
    4 6 4 0 
    5 9 7 0 
    5 7 4 0 
    7 9 6 0 
    8 9 7 0  
    '''
    ## ADD EDGES
    net.addLink( s1, s10 )
    net.addLink( s1, s5 )
    net.addLink( s1, s3 )
    net.addLink( s2, s9 )
    net.addLink( s3, s7 )
    net.addLink( s4, s10 )
    net.addLink( s4, s9 )
    net.addLink( s4, s6 )
    net.addLink( s5, s8 )
    net.addLink( s5, s7 )
    net.addLink( s6, s10 )
    net.addLink( s6, s8 )
    net.addLink( s8, s10 )
    net.addLink( s9, s10 )
    
    
    h1 = net.addHost( 'h1' , ip = '10.0.0.20')
    h2 = net.addHost( 'h2' , ip = '10.0.0.21')
    h3 = net.addHost( 'h3' , ip = '10.0.0.22')
    h4 = net.addHost( 'h4' , ip = '10.0.0.23')
    h5 = net.addHost( 'h5' , ip = '10.0.0.24')
    h6 = net.addHost( 'h6' , ip = '10.0.0.25')
    h7 = net.addHost( 'h7' , ip = '10.0.0.26')
    h8 = net.addHost( 'h8' , ip = '10.0.0.27')
    h9 = net.addHost( 'h9' , ip = '10.0.0.28')

    net.addLink( s1, h1)
    net.addLink( s2, h2)
    net.addLink( s3, h3)
    net.addLink( s4, h4)
    net.addLink( s5, h5)
    net.addLink( s6, h6)
    net.addLink( s7, h7)
    net.addLink( s8, h8)
    net.addLink( s9, h9)
    

    #Uncomment the following line to allow NAT - Can be used to connect hosts to SDN Controller
    #net.addNAT().configDefault()
    net.start()

    #Uncomment the following line to configure the GRE tunnel
    #s3.cmd('ovs-vsctl add-port s3 s3-gre1 -- set interface s3-gre1 type=gre options:remote_ip='+NODE2_IP)
    
    CLI( net )
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )
    emptyNet()
