'''
Code to unify vbulletin topics
'''

import argparse
from vbulletin import vbulletin

if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('urls_list', metavar = 'urls-list')
		parser.add_argument('--unfriendly', action = 'store_true', default = False, dest = 'unfriendly')

		return parser.parse_args()

	parsed = parse_cmdline_args()

	with open(parsed.urls_list, 'r') as urls_list_handle:
		for new_url in urls_list_handle:
			if parsed.unfriendly:
				print vbulletin.vbulletin_unfriendly_topics(new_url.strip())

