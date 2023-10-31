# DBLP Search Engine
## Directions
1. Download dblp.xml and dblp.dtd from https://dblp.uni-trier.de/xml/.
2. Create a "resources" directory and place dblp.xml and dblp.dtd in resources/.
3. (if downloading lucene from scratch) Download lucene-9.8.0 from https://lucene.apache.org/ and link its jaras to the dblp-search-engine project (https://www.jetbrains.com/help/idea/working-with-module-dependencies.html)
4. Include the following with your Java JVM options: `-DentityExpansionLimit=10000000`
5. Run Main.java and follow the prompts!