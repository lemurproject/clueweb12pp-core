'''
Main entry point to IPBoard-Source
'''
import argparse
import sys
from ipboard import ipboard

if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument(
			'seeds_list',
			metavar = 'seeds-list',
			help = '/path/to/seeds-list-files'
		)
		parser.add_argument(
			'-u',
			'--unfriendly',
			dest = 'unfriendly',
			action = 'store_true',
			default = False,
			help = 'Process an unfriendly seeds file'
		)
		parser.add_argument(
			'-m',
			'--mod-rewrite',
			dest = 'mod_rewrite',
			action = 'store_true',
			default = False,
			help = 'Process a mod rewrite seeds file'
		)

		return parser.parse_args()

	parsed = parse_cmdline_args()

	with open(parsed.seeds_list, 'r') as seeds_list_handle:
		for seed in seeds_list_handle:
			if parsed.unfriendly:
				print ipboard.process_unfriendly_topic_urls(seed.strip())

			if parsed.mod_rewrite:
				print ipboard.process_mod_rewrite_topic_urls(seed.strip())

			sys.stdout.flush()