'''Code to process index pages'''

import argparse
from phpbb import phpbb

if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('list_of_links', metavar = 'list-of-links')
		parser.add_argument('--unfriendly', dest = 'unfriendly', action = 'store_true', default = False)

		return parser.parse_args()

	parsed = parse_cmdline_args()

	with open(parsed.list_of_links, 'r') as list_of_links_handle:
		for new_link in list_of_links_handle:
			if parsed.unfriendly:
				print phpbb.process_unfriendly_forum_urls(new_link.strip())