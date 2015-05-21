__author__ = 'jp242'

def recommend(topic,entity,filelist,fileDir,outfile):
    topic = str(topic)
    model = open(filelist,'r')
    line = model.readline()
    found = False
    outf = open(outfile,'w')
    while line and not found:
        if line.replace("\n","") == topic:
            found = True
            line = model.readline()
            docs = line.split('\t')
            for doc in docs:
                if "<DELIM>" in doc:
                    spl = doc.split("<DELIM>")
                    dName = spl[0]
                    url = spl[1]
                    f = open(fileDir + "/" + dName,'r')
                    fStr = f.read()
                    if entity in fStr:
                        outf.write(dName + "\t" + url + "\n")
                    f.close()
        else:
            line = model.readline()
    model.close()
    outf.close()



if __name__ == "__main__":
    topic = 49
    entity = "_location"
    filelist = "/Volumes/BackupHD/Projects/Lumi/NER-50/fileList/entity-top-doc-url.txt"
    fileDir = "/Volumes/BackupHD/Projects/Lumi/NER-50/inputDocs/parsed"
    outfile = "/Volumes/BackupHD/Projects/Lumi/NER-50/recommendations/loc-recs.txt"
    recommend(topic,entity,filelist,fileDir,outfile)