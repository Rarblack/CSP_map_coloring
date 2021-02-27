import re


class Parser:
    def __init__(self, filename):
        self.__filename           = filename.strip()

        self.__variables          = None
        self.__domains            = None
        self.__arcs               = None


    def parse(self):
        switch = None

        with open(self.__filename) as file:
            for line in file.readlines():
                data = line.strip("\n")
                
                try:
                    if re.match(r'#', data):
                        raise
                    
                    data = data.split(",")

                    if switch == "variables":
                        self.__variables = [variable for variable in data]
                    elif switch == "domains":
                        self.__domains = [value for value in data]
                    elif switch == "arcs":
                        self.__arcs = [(pair.split(" ")[0], pair.split(" ")[1]) for pair in data]
                
                except:
                    data = data.strip('#').strip().lower()
                    if re.match("^(variables|domains|arcs)$", data):
                        switch = data


    def get_variables(self):
        return self.__variables

    def get_domains(self):
        return self.__domains

    def get_arcs(self):
        return self.__arcs