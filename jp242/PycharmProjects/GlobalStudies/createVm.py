__author__ = 'jp242'

def createVM(inputFile,outputFile):
    input = open(inputFile,'r')
    output = open(outputFile,'w')
    output.write("#set($langauges = [\"--Select--\",")
    line = input.readline()
    count = 0
    while line:
        count += 1
        line = line.replace("\n","")
        lineList = line.split()
        line = lineList[1]+"-"+lineList[0]
        newLine = "\"" + line + "\""
        line = input.readline()
        if not line:
            output.write(newLine)
        else:
            output.write(newLine + ",")
    print count
    output.write("])")
    output.close()
    input.close()


if __name__ == "__main__":
    input = "/Volumes/LocalDataHD/jp242/Documents/Projects/GlobalStudies/Implementation/langauges.txt"
    output = "/Volumes/LocalDataHD/jp242/Documents/Projects/GlobalStudies/Implementation/solr-global/example/solr/collection1/conf/velocity/languages.vm"
    createVM(input,output)