def satisfied(variable, neighbor, assignment):
    if neighbor not in assignment:
        return True
    return assignment[variable.get_name()] == assignment[neighbor.get_name()]

def consistent(csp, variable, assignment):
    for neighbor in variable.get_neighbors():
        if not satisfied(variable, neighbor, assignment):
            return False
    return True


def backtracking_search(csp):
    return backtrack({}, csp)

def backtrack(assignment, csp):
    # if assignment is complete then return assignment
    if len(assignment) == len(csp["variables"]):
        return assignment

    # this will be changed to lmi
    unassigned = [var for var in csp['variables'] if var not in assignment]

    # getting the first unassigned variable
    first = csp["variables"][unassigned[0]]
    for color in first.get_domains():
        assignment[first] = color
        if consistent(csp, first, assignment):
            result = backtrack(assignment, csp)
            if result:
                return result

        assignment.remove(first)
    return False



# the below 2 fuctions is responsible of arc consistency
def ac_3(csp):

    variables   = csp['variables']
    arcs        = csp['arcs']

    queue = [(variables[x_i], variables[x_j]) for x_i, x_j in arcs]

    while queue:
        (x_i, x_j) = queue.pop(0)
        
        # check whether there is a change in x domain
        if revise(x_i, x_j):
            
            # if there is no element left in x domains that means that the problem is not arc consistent and no need to continue 
            if not x_i.get_domains():  
                return False

            queue += [(x_k, x_i) for x_k in filter(lambda x: not x == x_j, x_i.get_neighbors())]
    
    return True


def revise(x_i, x_j):
    """
    Checks whether there is any value in the x and y domains that violates the rules. basically compares each values of x domain with y domain values
    """

    revised = False

    for color_i in x_i.get_domains():
        satisfied = False
        
        for color_j in x_j.get_domains():
            if not color_i == color_j: 
                satisfied = True

        if not satisfied:
            x_i.remove_from_domain(color_i)
            revised = True

    return revised


from custom_parser import Parser
from variable import Variable


if __name__ == "__main__":
    parser = Parser("input.txt")
    parser.parse()

    # getting read data
    variables   = parser.get_variables()
    domains     = parser.get_domains()
    arcs        = parser.get_arcs()

    # creating variable objects
    variable_objects = {var: Variable(var, domains) for var in variables}

    for x_i, x_j in arcs:
        variable_objects[x_i].add_neighbor(variable_objects[x_j])
        variable_objects[x_j].add_neighbor(variable_objects[x_i])
    
    # creating csp dictionary
    csp = {
        "variables": variable_objects,
        "arcs": arcs
    }

    # calling ac_3 to check arc consistency
    ac_3(csp)

    assignment = backtracking_search(csp)
    print(assignment)