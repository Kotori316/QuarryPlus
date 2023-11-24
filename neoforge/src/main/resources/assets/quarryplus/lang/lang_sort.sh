for file in $(ls *.json) ; do
    content=$(cat $file)
    echo $content | jq -S > $file
    echo "Done $file"
done
