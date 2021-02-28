def order_domain_values(csp, variable, assignment):
    """
    The least-constraining-value (LCV) heuristic is used to sort the list of values during backtracking in the order of the least constraining to most constraining. 
    This property is measured by how many values a given value "rules out" among other variables. 
    In other words, the least constraining value would be a value that, when assigned to a variable, would give other variables the maximum number of options to choose from. 
    """
    # dictionary to store occurence count of each color in other domains
    color_occurence_count = {}

    # looping through all possible values in variable domain
    for color in variable.get_domains():

        # to count the occurence
        counter = 0

        # looping through all neighbors(since we have only single contraint)
        for neighbor in variable.get_neighbors():

            # if color is in other domain
            if color in neighbor.get_domains():

                # increment the counter
                counter += 1
        
            color_occurence_count[color] = counter


    return [color[0] for color in sorted(color_occurence_count.items(), key=lambda item: item[1])]


def count_constraints(variable):
    return len(variable.get_neighbors())

def select_unassigned_variable(csp, assignment):
    """
    The minimum-remaining-values (MRV) heuristic chooses the next variable to seek assignment by examining how many constraints that variable imposes on remaining variables, and choosing the one with the most constraints. 
    If there is a tie, a degree heuristic is used to determine which variable will be chosen.
    """
    # get all unassigned variables
    variable = [var for var in csp['variables'].values() if var not in assignment.keys()]

    min = variable[0]
    
    for variable in variable:

        # possible size of the domain of the variable
        variable_domain_size    = len(variable.get_neighbors())
        min_domain_size         = len(min.get_neighbors())
        
        if variable_domain_size < min_domain_size:
            min = variable
        elif variable_domain_size == min_domain_size:
            if count_constraints(variable) > count_constraints(min):
                min = variable
    
    return min


def satisfied(variable, neighbor, assignment):
    if neighbor not in assignment:
        return True
    return not assignment[variable] == assignment[neighbor]

def consistent(csp, variable, assignment):
    for neighbor in variable.get_neighbors():
        if not satisfied(variable, neighbor, assignment):
            return False
    return True


def backtracking_search(csp):
    return backtrack(csp, {})

def backtrack(csp, assignment):

    # if assignment is complete then return assignment
    if len(assignment) == len(csp["variables"]):
        return assignment

    # choosing an unassinged variable lmi
    variable = select_unassigned_variable(csp, assignment)

    # looping through available domain colors
    for color in order_domain_values(csp, variable, assignment):

        # assigning color to the first choosen variable
        assignment[variable] = color
        
        # consistency check whether the chosen coler does not violate any rule
        if consistent(csp, variable, assignment):

            # 
            result = backtrack(csp, assignment)
            
            # if there is a result, return it
            if result:
                return result

        # the assignment was not correct so, it needs to be erased
        assignment.pop(variable, None)
    return False


# the below 2 fuctions is responsible of arc consistency
def ac_3(csp):

    variables   = csp['variables']
    arcs        = csp['arcs']

    queue       = [(variables[x_i], variables[x_j]) for x_i, x_j in arcs]

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





# ---------------------------------------------MAIN 
from custom_parser import Parser
from variable import Variable
import sys

if __name__ == "__main__":

    filename = sys.argv[1]

    parser = Parser(filename)
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

    if assignment:
        for k, v in assignment.items():
            print(f'{k.get_name()}: {v}')
    else:
        print("NO SOLUTION")