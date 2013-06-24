import sys
from nabble import nabble_final_urls_list

filename = sys.argv[1]

with open(filename, 'r') as filename_handle:
	for new_line in filename_handle:
		print nabble_final_urls_list.topic_url_before_redirect(new_line)