#!/opt/local/bin/python
# -*- coding: utf8 -*-
import sys
import pydot
import re

g = pydot.graph_from_dot_file(sys.argv[1])
destFile = sys.argv[2]

template = """
\\begin{pspicture}(30,30)
%s
%s
\\end{pspicture}
"""

SCALE = 0.05

nodeStr = ""
for node in g.get_node_list():
  if not (node.get("pos") is None):
    x,y = map(float,node.get("pos").replace('"','').split(","))
    name = node.get("label")

    # replace labels with latex commands
    name = name.replace('{','\{')
    name = name.replace('}','\}')
    
    # process fractions
    while name.find('/') >= 0:
      strs = name.split('/',1)
      result1 = re.match('(.*?)(\d*)\Z',strs[0])
      result2 = re.match('(\d*)(.*)',strs[1])
      name = result1.group(1) + "\\FracGame{%s}{%s}"%(result1.group(2),result2.group(1)) + result2.group(2)

    # process PowTo(...)  NOTE: falar com Carlos para perceber melhor esta notacao!
    while name.find('PowTo') >= 0:
      strs = name.split('PowTo(',1)       
      result1 = re.match('(.*?)(,)(.*?)(\))(.*)\Z',strs[1])
      name = strs[0] + '\\POWTO{' + result1.group(1) + "}{" +  result1.group(3) + "}" + result1.group(5)
    
    # process Pow(...)    
    while name.find('Pow') >= 0:
      strs = name.split('Pow(',1)       
      result1 = re.match('(.*?)(,)(.*?)(\))(.*)\Z',strs[1])
      name = strs[0] + '\\POW{' + result1.group(1) + "}{" +  result1.group(3) + "}" + result1.group(5)
    name = name.replace('POW', 'PowGame')

    name = name.replace('PowTO', 'PowToGame') # this cannot be done before!
    
    # process Tiny(...)    
    while name.find('Tiny') >= 0:
      strs = name.split('Tiny(',1)       
      result1 = re.match('(.*?)(\))(.*)\Z',strs[1])
      name = strs[0] + '\\TINY{' + result1.group(1) + "}" + result1.group(3)
    name = name.replace('TINY', 'TinyGame')
      
    # process Miny(...)    
    while name.find('Miny') >= 0:
      strs = name.split('Miny(',1)       
      result1 = re.match('(.*?)(\))(.*)\Z',strs[1])
      name = strs[0] + '\\MINY{' + result1.group(1) + "}" + result1.group(3)
    name = name.replace('MINY', 'MinyGame')

    name = name.replace('^','\\UpGame')
    name = name.replace('v','\\DownGame')
    name = name.replace('*','\\StarGame')
    name = name.replace('+-','\\MorelessGame')
    name = name.replace('|||','\,\mid\mid\mid\,')
    name = name.replace('||','\,\mid\mid\,')
    name = name.replace('|','\,\mid\,')
    
    nodeStr += ("\\rput(%s,%s){\\rnode{%s}{$%s$}}\n"%(x*SCALE,y*SCALE,node.get_name(),name)).replace('"','')
  
edgeStr = ""
for edge in g.get_edge_list():
  sName = edge.get_source()
  dName = edge.get_destination()
  
  s = g.get_node( sName )[0]
  d = g.get_node( dName )[0]

  if not (s.get("pos") is None) and not (d.get("pos") is None):
    edgeStr += ("\\ncLine[nodesep=3pt]{->}{%s}{%s}\n"%(s.get_name(),d.get_name())).replace('"','')
  
df = open(destFile,"w")
df.write( template%(nodeStr,edgeStr) )
df.close()