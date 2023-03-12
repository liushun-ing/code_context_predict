# Graduation project idea plugin



## Introduction

The plugin captures idea developer's interesting code element, and predicts what code they want to edit next, and provides a table for them to navigate code directly.



## Project Explanation

### VF3

**What is VF3?**

VF3 is a graph matching algorithm, specialized in solving graph isomorphism and graph-subgraph isomorphism. In particular, it is able to solve both testing and listing problems, being able to determine not only if the isomorphism exists (testing) but also where and how many times a pattern graph is present inside the target graph (listing).

**How to use?**

The invocation entry is the `MainEntry` class in the `algorithm` folder.

```java
// new entry object
MainEntry mainEntry = new MainEntry();
// execute algorithm
ArrayList<ArrayList<Solution>> executeResult = mainEntry.execute(targetGraph);
// do your next operation...
```



### MyJstereoCode

**What is it?**

A tool that automatically identifies the stereotypes of methods and classes in Java systems.

**How to use?**

The invocation entry is the `StereotypeAssigner` class in the folder `myjstereocode/entry`.

There are two steps to do:

1. compute project information(the class is `ProjectInformation` in the `myjstereocode/info`)
2. call assigner to get stereotype


```java
// compute information and construct a assigner
DataCenter.PROJECT_INFORMATION = new ProjectInformation(project);
DataCenter.STEREOTYPE_ASSIGNER = new StereotypeAssigner();
DataCenter.STEREOTYPE_ASSIGNER.setParameters(DataCenter.PROJECT_INFORMATION.getMethodsMean(), DataCenter.PROJECT_INFORMATION.getMethodsStdDev());
// call assign method
String stereotype = DataCenter.STEREOTYPE_ASSIGNER.assignStereotypes(v.getPsiElement());
```



### Data

This package is used to store plugin's important data and constants, and also define the Entity used in the plugin.



### Operation

This package encapsulates the main logic for handling the data.

- `TableDataOperator`: this operator is used to handle transactions of prediction table data, such as executing VF3 matching algorithm, transfering result solutions to object shown on table, and sorting table data according to confidence and so on. 
- `TargetGraphOperator`: this operator is used to build target graph and expand target graph within prediction stepn using psi-tree.
- `TreeDataOperator`: this operator is used to handle changes of context tree data, such as adding interesting psi element and adjusting tree structure.



### Plugin

This package documents the implementation logic of the idea plug-in.

- `listener`: some idea action's listener to monitor the occurrence of events
- `model`: this is an implementation of <code>TableModel</code>  to store the cell value objects.
- `render`: cell rendering rules
- `window`: toolWindow's initial logic and toolWindow's events handle logic.