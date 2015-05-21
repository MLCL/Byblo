__author__ = 'jp242'


def splitdocs(inFileLoc,outLoc):
    indoc = open(inFileLoc,'r')
    line = indoc.readline()
    i = 0
    newDoc = open(outLoc+"/"+str(i)+".txt",'w')
    while line:
        if line == "\n":
            newDoc.close()
            i += 1
            newDoc = open(outLoc+"/"+str(i)+".txt",'w')
        else:
            newDoc.write(line)
        line = indoc.readline()

if __name__ == "__main__":
    inFile = "/Volumes/LocalDataHD/jp242/PycharmProjects/Global-SolrPy/sasho-doc"
    outLoc = "/Volumes/LocalDataHD/jp242/Documents/sasho-docs/out"
    splitdocs(inFile,outLoc)