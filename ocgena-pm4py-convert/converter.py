import builtins

import pm4py
import itertools
import yaml

from pm4py.objects.petri_net.obj import PetriNet


# place_types:
#     per_place:
#         "p1": "INPUT"
#         "p2": "OUTPUT"
#         "o1": "INPUT"
#         "o2": "OUTPUT"

def sink_name(type: str):
    return '{}-sink'.format(type)


def source_name(type: str):
    return '{}-source'.format(type)


def make_place_name(type: str, place_name: str):
    if place_name == "sink":
        name = sink_name(type)
    elif place_name == "source":
        name = source_name(type)
    else:
        name = place_name
    return name.replace("_", "-")


def make_arc_name(type: str, arc: PetriNet.Arc):
    source = arc.source

    if isinstance(source, PetriNet.Place):
        source_name = make_place_name_place(type, source)
    elif isinstance(source, PetriNet.Transition):
        source_name = make_transition_name(source)
    else:
        raise Exception("unsupported source exception {}".format(source))

    target = arc.target
    if isinstance(target, PetriNet.Place):
        target_name = make_place_name_place(type, target)
    elif isinstance(target, PetriNet.Transition):
        target_name = make_transition_name(target)
    else:
        raise Exception("unsupported source exception {}".format(source))

    return "{}_{}".format(source_name, target_name)


def map_arc(type: str, arc: PetriNet.Arc):
    return {
        "type": "arc",
        "id": make_arc_name(type, arc)
    }


def make_place_name_place(type: str, place: PetriNet.Place):
    if place is None:
        return None
    return make_place_name(type, place.name)


def make_transition_name(transition: PetriNet.Transition):
    if transition.label is not None:
        transition_name = transition.label
    else:
        transition_name = transition.name
    return transition_name.replace("_", "-")


def map_transition(type: str, transition: PetriNet.Transition):
    transition_name = make_transition_name(transition)
    from_places = get_transition_in_places(type, transition)
    to_places = get_transition_out_places(type, transition)
    return {
        "type": "transition",
        "id": transition_name,
        "label": transition_name,
        "from_places": from_places,
        "to_places": to_places
    }


def map_place(type: str, place: PetriNet.Place):
    place_name = make_place_name_place(type, place)
    from_transitions = [make_transition_name(arc.source) for arc in list(place.in_arcs) if arc.source is not None]
    to_transitions = [make_transition_name(arc.target) for arc in list(place.out_arcs) if arc.target is not None]

    return {
        "type": "place",
        "id": place_name,
        "label": place_name,
        "from_transtns": from_transitions,
        "to_transtns": to_transitions
    }


def get_transition_in_places(type: str, transition: PetriNet.Transition):
    return [make_place_name_place(type, arc.source) for arc in list(transition.in_arcs) if
            arc.source is not None]


def get_transition_out_places(type: str, transition: PetriNet.Transition):
    return [make_place_name_place(type, arc.target) for arc in list(transition.out_arcs) if arc.target is not None]


def update_transition_with(obj, type: str, other_subnet_transition: PetriNet.Transition):
    from_places = get_transition_in_places(type, other_subnet_transition)
    to_places = get_transition_out_places(type, other_subnet_transition)

    obj["from_places"] = [i for source in [obj["from_places"], from_places] for i in source]
    obj["to_places"] = [i for source in [obj["to_places"], to_places] for i in source]


def main():
    ocel = pm4py.read.read_ocel2_json("order-management.json")

    # Get the list of object types in the OCEL
    object_types = pm4py.ocel_get_object_types(ocel)
    print("Object types: ", object_types)

    # Return an OCEL with a subset of randomly chosen objects
    sampled_ocel = pm4py.sample_ocel_objects(ocel, 50)

    # Discover an Object-Centric Petri Net (OC-PN) from the sampled OCEL
    ocpn = pm4py.discover_oc_petri_net(sampled_ocel)

    # Get the set of activities for each object type
    ot_activities_sampled = pm4py.ocel_object_type_activities(sampled_ocel)
    print("Activities per object types in the sampled OCEL: ", ot_activities_sampled)

    # Count for each event the number of objects per type
    objects_ot_count_sampled = pm4py.ocel_objects_ot_count(sampled_ocel)
    print("Number of related objects per type in the sampled OCEL: ", objects_ot_count_sampled)

    per_object_type_id = {}

    for obj_type in list(ocpn["object_types"]):
        per_object_type_id[obj_type] = { "label" : obj_type}

    per_place = {}
    for type in ocpn['petri_nets']:
        petri_net = ocpn['petri_nets'][type]

        places = petri_net[0].places
        for place in places:
            place_type = None
            if place.name == "sink":
                place_type = "OUTPUT"
            elif place.name == "source":
                place_type = "INPUT"

            if place_type is not None:
                place_name = make_place_name(type, place.name)
                per_place[place_name] = place_type

    place_types = {"per_place": per_place}

    per_place = {}
    for type in ocpn['petri_nets']:
        petri_net = ocpn['petri_nets'][type]
        places = petri_net[0].places
        for place in places:
            place_name = make_place_name(type, place.name)
            per_place[place_name] = type
    place_object_types = {"per_place": per_place}

    # per_id = {}
    #
    # transitions = {}
    # for type in ocpn['petri_nets']:
    #     petri_net = ocpn['petri_nets'][type]
    #     transition_per_type = []
    #     for transition in list(petri_net[0].transitions):
    #         transition_per_type.append(make_transition_name(transition))
    #     transitions[type] = transition_per_type
    #
    # transitions_per_type_duplicate_removed = {}
    #
    # for type in transitions:
    #     per_type_transitions = transitions[type]
    #     removed = list(set(per_type_transitions))
    #     transitions_per_type_duplicate_removed[type] = removed

    total_places = {}
    total_transitions = {}
    total_arcs = {}

    for type in ocpn['petri_nets']:
        petri_net = ocpn['petri_nets'][type]
        for transition in list(petri_net[0].transitions):
            transition_name = make_transition_name(transition)
            if transition_name not in total_transitions:
                total_transitions[transition_name] = map_transition(type, transition)
            else:
                existing = total_transitions[transition_name]
                update_transition_with(existing, type, transition)

    for type in ocpn['petri_nets']:
        petri_net = ocpn['petri_nets'][type]

        for place in list(petri_net[0].places):
            place_name = make_place_name_place(type, place)
            total_places[place_name] = map_place(type, place)

    for type in ocpn['petri_nets']:
        petri_net = ocpn['petri_nets'][type]

        for arc in list(petri_net[0].arcs):
            arc_name = make_arc_name(type, arc)
            total_arcs[arc_name] = map_arc(type, arc)

    per_id_petri_atoms = {}

    for i in total_transitions:
        per_id_petri_atoms[i] = total_transitions[i]
    for i in total_places:
        per_id_petri_atoms[i] = total_places[i]
    for i in total_arcs:
        per_id_petri_atoms[i] = total_arcs[i]

    ocnet = {
        "place_object_types": place_object_types,
        "place_types": place_types,
        "object_types": {"per_object_type_id": per_object_type_id},
        "petri_atoms": per_id_petri_atoms
    }


    with open('ocnet.yaml', 'w') as file:
        yaml.dump(ocnet, file,)

    debugging = True

    # Get a visualization of the OC-PN (Returns a Graphviz digraph)
    # gph = pm4py.visualization.ocel.ocpn.visualizer.apply(ocpn)

    # View the diagram using matplotlib
    # pm4py.visualization.ocel.ocpn.visualizer.matplotlib_view(gph)


main()
