'''
Program to process the list of nabble posts and then produce a list of forum
init URLs
'''

import argparse
import re


def get_nabble_base_topic_link(link):
	parts = re.match(r'(.*-td\d+)(i\d+)?.html', link)

	return '%(base_link)s.html' % {'base_link' : parts.group(1)}

if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('nabble_posts', metavar = 'nabble-posts')

		return parser.parse_args()

	parsed = parse_cmdline_args()

	with open(parsed.nabble_posts, 'r') as nabble_posts_handle:
		for link in nabble_posts_handle:
			print get_nabble_base_topic_link(link.strip())