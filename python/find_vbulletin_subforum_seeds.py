'''
List of vbulletin subforums
'''

import argparse
from vbulletin import vbulletin


if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('vbulletin_list', metavar = 'vbulletin-list', help = 'vbulletin seeds list')
		parser.add_argument(
			'--unfriendly',
			dest = 'unfriendly',
			default = False,
			action = 'store_true',
			help = 'Handle unfriendly URLs'
		)

		return parser.parse_args()

	parsed = parse_cmdline_args()

	with open(parsed.vbulletin_list, 'r') as vbulletin_list_handle:
		for url in vbulletin_list_handle:
			try:
				if parsed.unfriendly:
					print vbulletin.process_unfriendly_subforum_urls(url.strip())
			except:
				pass