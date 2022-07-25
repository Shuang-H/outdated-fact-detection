# Outdated Fact Detection in KBs

This is the code for the paper: [Outdated Fact Detection in Knowledge Bases](https://ieeexplore.ieee.org/document/9101535),
Shuang Hao, Chengliang Chai, Guoliang Li, Nan Tang, Ning Wang, Xiang Yu,
which was presented at [ICDE 2020](https://icde.utdallas.edu/) as a short paper.

## Framework

![](https://github.com/Shuang-H/outdated-fact-detection/blob/main/framework.jpg)

- Phase I:  Outdated Fact Prediction. The input is the KB and reference data on hand, and the output is the OD Model that predicts the likelihood of each fact being outdated, which provides evidence for Phase II.
- Phase II: Human-based Verification. It takes the KB, logical rules, and prediction results of OD Model as the input to build a graph model for selecting facts to be verified, and outputs the human answers of the selected facts.
- Phase III: Rule-based Fact Expansion. It takes the human labels from  Phase II as input, infers more outdated facts, and feeds these inferred facts into the ML model (Phase I).

Here, we provide the code of `'Top-k Hits Selection & Fact Expansion'` and `'Logical Rule Mining'`

## Datasets
In our experiments, the revisions from Yago-1.0.0 to Yago-1.1.0 were used as the KB revision history to mine logical rules and Yago-2.0.0 was considered as the ground truth after we detected the outdated facts in Yago-1.1.0. They can be downloaded from https://www.mpi-inf.mpg.de/departments/databases-and-information-systems/research/yago-naga/yago/archive.

DBpedia was also used. The revisions before DBpedia-3.4 were used as the KB revision records and DBpedia-3.6 was taken as the ground truth after we detected the outdated facts in DBpedia-3.4. They can be downloaded from http://downloads.dbpedia.org.



## Citation

If you find this code useful in your research then please cite
````
@inproceedings{hao2020outdated,
  title={Outdated Fact Detection in Knowledge Bases},
  author={Hao, Shuang and Chai, Chengliang and Li, Guoliang and Tang, Nan and Wang, Ning and Yu, Xiang},
  booktitle={2020 IEEE 36th International Conference on Data Engineering (ICDE)},
  pages={1890--1893},
  year={2020},
  organization={IEEE}
}
````
