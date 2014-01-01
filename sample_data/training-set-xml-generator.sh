#!/bin/bash

trainingset_filename="training-set.xml"

current_dir=`pwd`
echo "Generating to $trainingset_filename ..."

rm $trainingset_filename
echo "<?xml version=\"1.0\"?>" >> $trainingset_filename
echo "<training-set>" >> $trainingset_filename

for path in $(ls -d orl_faces/*/)
do
	personId=`basename $path`
	
	echo "    <person id=\"$personId\">" >> $trainingset_filename
	
	for file in $(ls $path)
	do
		filePath=${path}${file}
		echo "        <photo>$filePath</photo>" >> $trainingset_filename
	done

	echo "    </person>" >> $trainingset_filename
done

echo "</training-set>" >> $trainingset_filename
