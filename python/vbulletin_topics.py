'''
Code to unify vbulletin topics
'''

import argparse
import sys
from vbulletin import vbulletin

if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('urls_list', metavar = 'urls-list', nargs = '?', default = None)
		parser.add_argument('--unfriendly', action = 'store_true', default = False, dest = 'unfriendly')

		return parser.parse_args()

	parsed = parse_cmdline_args()
	sys.stderr = open('err_file', 'w+')

	if parsed.urls_list:
		with open(parsed.urls_list, 'r') as urls_list_handle:
			for new_url in urls_list_handle:
				if parsed.unfriendly:
					try:
						print vbulletin.vbulletin_unfriendly_topics(new_url.strip())
					except Exception as e:
						sys.stderr.write(e.message)


	else:
		# listen to stdin
		for new_url in sys.stdin:
			if parsed.unfriendly:
				fixed_url = vbulletin.vbulletin_unfriendly_topics(new_url.strip())
				if fixed_url:
					print fixed_url