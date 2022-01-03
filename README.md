# HAI914  - Mini Star Query Engine

## Description

This project aims at implementing a mini star query engine. Done during our second year of master in Software Engineering at the Faculty of Sciences of Montpellier (2021-2022)

## Students

| 🎓 Name                | 📧 Email                                 | 🏷️ Student number |
| -------------------- | -------------------------------------- | ---------------- |
| **CANTA** Thomas     | thomas.canta@etu.umontpellier.fr       | 21607288         |
| **FONTAINE** Quentin | fontaine02.quentin@etu.umontpellier.fr | 21611404         |

## Installation

```
git clone git@github.com:DocAmaroo/MiniStarQueryEngine.git
cd MiniStarQueryEngine
```

## Execution

### JAR

Here is the main command line to use to execute the *.jar* file:

```bash
java -jar qengine.jar [OPTIONS]
```

The options available are listed [here](#Options)

<details><summary>See execution samples</summary>
<br/>

Execute the query engine with the dataset and the queries given:
```
java -jar qengine.jar -data ~/data/sample_data.nt -queries ~/data/sample_query.queryset
```

Execute the query engine with the dataset and the queries given by using the working directory. The system will not warmup and also add a benchmark with Jena:
```
java -jar qengine.jar -workingDir ~/data -data sample_data.nt -queries sample_query.queryset -nowarmup -jena
```
</details>

### Alternative

👉 Open the project on Intellij or Eclipse and add the options as arguments on run.

### Options

Here is the available options you can use to execute the program:

* `-help` &rarr; show this message.
* `-workingDir` <path/to/dir> &rarr; path to the directory containing queries or/and data. This value is optional.
* `-queries` <path/to/file> &rarr; absolute path to the queries file, or the relative from a working directory specified.
* `-data` <path/to/file> &rarr; absolute path to the data file, or the relative from a working directory specified.
* `-output` <path/to/dir> &rarr; set the log output directory. By default is <path/to/qengine.jar>/output.
* `-rmd` <filename> &rarr; save on the path give the queries without duplicates. By default save on workingDir/noDuplicates/filenam.
* `-verbose` &rarr; print all information during execution process on the console. (tips: doesn't affect logs output.
* `-jena` &rarr; execute Jena on the data and queries given.
* `-nowarmup` &rarr; allow to desactivate the warmup.
