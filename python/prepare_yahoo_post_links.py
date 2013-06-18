'''
Code to generate post links for the ygroups pages
'''

import argparse
import re


MESSAGE_QUERY_PART = '?threaded=1&m=e&var=1&tidx=1'
MESSAGE_REGEX = r'(.*)/(message)/(\d+)'

def process_message_link(link):
	found = re.search(MESSAGE_REGEX, link)
	
	first_part = found.group(1)
	message_part = 'messages'
	post_id = found.group(3)

	return '%(first_part)s/%(message_part)s/%(post_id)s%(query_part)s' % {
		'first_part' : first_part,
		'message_part' : message_part,
		'post_id' : post_id,
		'query_part' : MESSAGE_QUERY_PART
	}

if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('posts_list', metavar = 'posts-list', help = 'List of posts')

		return parser.parse_args()

	parsed = parse_cmdline_args()

	with open(parsed.posts_list, 'r') as posts_list_handle:
		for link in posts_list_handle:
			print process_message_link(link.strip())