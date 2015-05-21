__author__ = 'jp242'

import os
import gzip

def removefirstByte(fileLoc,outDir,readSize=1):
    foo = open(fileLoc,'rb')
    out = open(outDir + "/" + os.path.basename(fileLoc),'w')
    print foo.read(1) + "start"
    print foo.read()
    out.write(foo.read())
    out.close()
    foo.close()

def processList(fileDir,outLoc,readSize=1):
    for f in os.listdir(fileDir):
        removefirstByte(fileDir + "/" + f,outLoc)


if __name__ == '__main__':
    readSize=1
    fileLoc = "/Users/jp242/Documents/Projects/Lumi/compressedWebpages"
    outLoc = "/Users/jp242/Documents/Projects/Lumi/outputPages"
    processList(fileLoc,outLoc,readSize)
