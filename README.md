# HAI914  - Mini Star Query Engine

## Description

This project aims at implementing a mini star query engine. Done during our second year of master in Software Engineering at the Faculty of Sciences of Montpellier (2021-2022)

## Students

| 🎓 Name                | 📧 Email                                 | 🏷️ Student number |
| -------------------- | -------------------------------------- | ---------------- |
| **CANTA** Thomas     | thomas.canta@etu.umontpellier.fr       | 21607288         |
| **FONTAINE** Quentin | fontaine02.quentin@etu.umontpellier.fr | 21614404         |

## Installation

```
git clone git@github.com:DocAmaroo/MiniStarQueryEngine.git
cd MiniStarQueryEngine
```

## Execution

### JAR

⚠️ The export of our project in .jar is currently unavailable. It is currently impossible to go through this step.

### Alternative

👉 Open the project on Intellij or Eclipse  
👉 On the run add the arguments below:

* `-help` &rarr; show this message;
* `-workingDir` <path/to/dir> &rarr; path to the directory containing queries or/and data. This value is optional;
* `-queries` <path/to/file> &rarr; absolute path to the queries file, or the relative from a working directory specified;
* `-data` <path/to/file> &rarr; absolute path to the data file, or the relative from a working directory specified;
* `-output` <path/to/dir> &rarr; set the log output directory. By default is <path/to/qengine.jar>/output;
* `-verbose` &rarr; print all information during execution process on the console. (tips: doesn't affect logs output)

<details><summary>See usage sample</summary>
<br/>

The two samples below or equivalent.

`-data ~/data/sample_data.nt -queries ~/data/sample_query.queryset -verbose`

`-workingDir ~/data -data sample_data.nt -queries sample_query.queryset -verbose`

</details><br/>