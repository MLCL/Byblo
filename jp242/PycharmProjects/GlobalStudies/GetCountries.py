__author__ = 'jp242'

def getCountries(inputFile,outputFile):
    input = open(inputFile,'r')
    output = open(outputFile,'w')
    line = input.readline()
    count = 0
    while line:
        if "name" in line:
            strs = line.split("name")
            strs = strs[1]
            strs = strs.split("}")
            strs = strs[0]
            strs = strs.replace(":","")
            strs = strs.replace("\"","")
            output.write(strs + "\n")
            count += 1
        line = input.readline()
    input.close()
    output.close()
    print count


if __name__ == "__main__":
    input = "/Volumes/LocalDataHD/jp242/Downloads/code/solr/solr/collection1/conf/velocity/world-countries.json"
    output = "/Volumes/LocalDataHD/jp242/Documents/Projects/GlobalStudies/Documents/countries.txt"
    getCountries(input,output)