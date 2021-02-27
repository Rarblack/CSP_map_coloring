class Variable:
    def __init__(self, name, domains):
        self.__name = name
        self.__domains = domains
        self.__color = None
        self.__neighbors = []


    def add_neighbor(self, neighbor):
        if not neighbor in self.__neighbors:
            self.__neighbors.append(neighbor)

    def remove_from_domain(self, color):
        self.__domain.remove(color)

    def compare_color(self, other):
        return self.__color == other.get_color()

    def get_name(self):
        return self.__name

    def get_color(self):
        return self.__color

    def get_neighbors(self):
        return self.__neighbors

    def get_domains(self):
        return self.__domains